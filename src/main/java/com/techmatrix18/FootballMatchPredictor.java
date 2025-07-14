package com.techmatrix18;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Predictor of Football match 🇪🇸
 *
 * @author Alexander Kuziv makklays@gmail.com
 * @since 14-07-2025
 * @version 0.0.1
 */

public class FootballMatchPredictor {
    public static void main(String[] args) {
        // Example of a training dataset
        // Входы: статистика матчей (удары, владение, фолы, угловые,..)
        // 1. shots_home - удары хозяев
        // 2. shots_away - удары гостей
        // 3. shots_on_target_home - удары в створ хозяев
        // 4. shots_on_target_away - удары в створ гостей
        // 5. possession_home - владение мячом (хозяева, в %)
        // 6. possession_away - владение мячом (гости, в %)
        // 7. corners_home - угловые хозяев
        // 8. corners_away - угловые гостей
        // 9. fouls_home - нарушения хозяев
        //10. fouls_away - нарушения гостей
        INDArray input = Nd4j.create(new double[][]{
                {10, 8, 5, 3, 55, 45, 6, 4, 12, 14}, // хозяева победили 2:1
                {8,  9, 3, 4, 48, 52, 3, 6, 10, 10}, // ничья 1:1
                {6, 11, 2, 6, 43, 57, 2, 7, 11,  8}, // гости победили 0:2
                {13, 5, 7, 2, 60, 40, 7, 3,  9, 13}, // хозяева победили 3:0
                { 7, 7, 3, 3, 50, 50, 4, 4, 11, 11}  // ничья 0:0
        });

        // Метки: исходы [победа хозяев, ничья, победа гостей]
        INDArray labels = Nd4j.create(new double[][]{
                {1, 0, 0}, // хозяева победили
                {0, 1, 0}, // ничья
                {0, 0, 1}, // победа гостей
                {1, 0, 0}, // хозяева победили
                {0, 1, 0}  // ничья
        });

        DataSet dataSet = new DataSet(input, labels);

        // Network configuration
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .updater(new Adam(0.01))
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(10)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(16)
                        .nOut(8)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(8)
                        .nOut(3)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        // Обучение
        for (int i = 0; i < 2000; i++) {
            model.fit(dataSet);
        }

        // Тестовый матч
        /*INDArray testInput = Nd4j.create(new double[][]{
                {9, 7, 4, 3, 53, 47, 5, 4, 10, 12}
        });*/

        // 🇪🇸 Estadísticas: 🏟️ Barcelona (local) - ⚔️ Real Madrid (visitante)
        // ⚽ Unos 295 partidos en total en todas las competiciones.
        INDArray testInput = Nd4j.create(new double[][]{
                {126, 101, 346, 348, 47.2, 36, 325, 303, 718, 839}
        });

        INDArray prediction = model.output(testInput);

        BigDecimal win  = new BigDecimal(prediction.getDouble(0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal draw = new BigDecimal(prediction.getDouble(1)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal loss = new BigDecimal(prediction.getDouble(2)).setScale(2, RoundingMode.HALF_UP);

        System.out.printf("Outcome probabilities (win/draw/loss): [%s, %s, %s]%n", win, draw, loss);
    }
}

