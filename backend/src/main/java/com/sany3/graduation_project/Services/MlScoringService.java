package com.sany3.graduation_project.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sany3.graduation_project.Repositories.UserBehaviorEventRepository;
import com.sany3.graduation_project.entites.UserBehaviorEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlScoringService {

    private final UserBehaviorEventRepository eventRepository;

    private double[] coefficients;
    private double intercept;
    private double[] scalerMean;
    private double[] scalerScale;
    private double threshold;
    private boolean modelLoaded = false;

    @PostConstruct
    public void loadModel() {
        try {
            ClassPathResource resource = new ClassPathResource("nudge_model_weights.json");
            if (!resource.exists()) {
                log.warn("nudge_model_weights.json not found in classpath — ML scoring disabled, rule engine will run without ML filter");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> weights = mapper.readValue(is, Map.class);

                List<Double> coefList  = (List<Double>) weights.get("coefficients");
                List<Double> meanList  = (List<Double>) weights.get("scaler_mean");
                List<Double> scaleList = (List<Double>) weights.get("scaler_scale");

                coefficients = coefList.stream().mapToDouble(Double::doubleValue).toArray();
                scalerMean   = meanList.stream().mapToDouble(Double::doubleValue).toArray();
                scalerScale  = scaleList.stream().mapToDouble(Double::doubleValue).toArray();
                intercept    = ((Number) weights.get("intercept")).doubleValue();
                threshold    = ((Number) weights.get("threshold")).doubleValue();

                modelLoaded = true;
                log.info("ML nudge model loaded successfully from nudge_model_weights.json");
            }
        } catch (Exception e) {
            log.warn("Failed to load ML model: {} — ML scoring disabled", e.getMessage());
        }
    }
    public boolean shouldNudge(Long userId) {
        if (!modelLoaded) {
            return true;
        }

        double[] features = extractFeatures(userId);
        double probability = sigmoid(dotProduct(features));

        log.debug("ML score for user {}: P(nudge)={} threshold={}",
                userId, String.format("%.2f", probability), threshold);
        return probability >= threshold;
    }
    private double[] extractFeatures(Long userId) {
        List<UserBehaviorEvent> events = eventRepository.findByUserIdOrderByOccurredAtDesc(userId);

        boolean openedAtNight     = false;
        boolean viewedServiceType = false;
        boolean startedForm       = false;
        int     appOpenCount      = 0;

        for (UserBehaviorEvent e : events) {
            switch (e.getEventType()) {
                case APP_OPENED -> {
                    appOpenCount++;
                    int hour = e.getOccurredAt().getHour();
                    if (hour >= 21 || hour <= 4) {
                        openedAtNight = true;
                    }
                }
                case SERVICE_TYPE_VIEWED     -> viewedServiceType = true;
                case SERVICE_REQUEST_STARTED -> startedForm = true;
                default -> {}
            }
        }
        double cappedOpenCount = Math.min(appOpenCount, 5);
        return new double[]{
            openedAtNight     ? 1.0 : 0.0,
            viewedServiceType ? 1.0 : 0.0,
            startedForm       ? 1.0 : 0.0,
            cappedOpenCount
        };
    }
    private double dotProduct(double[] rawFeatures) {
        double result = intercept;
        for (int i = 0; i < rawFeatures.length; i++) {
            double scale = scalerScale[i] == 0.0 ? 1.0 : scalerScale[i];
            double scaled = (rawFeatures[i] - scalerMean[i]) / scale;
            result += coefficients[i] * scaled;
        }
        return result;
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }
}
