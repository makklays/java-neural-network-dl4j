package com.techmatrix18.controllers.api;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
//import org.deeplearning4j.nn.conf.updater.Adam;  // Utilizo Adam
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API and URLs for Neural Network
 *
 * @author Alexander Kuziv makklays@gmail.com
 * @since 01-07-2025
 * @version 0.0.1
 */

@RestController
public class NeuralNetworkController {

    private static MultiLayerNetwork createModel() {
        NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
                //.seed(123)
                //.updater(new Adam(0.001))
                //.updater(new Sgd(0.01))
                .list();

        // Capa oculta de entrada
        listBuilder.layer(0, new DenseLayer.Builder()
                .nIn(2)
                .nOut(2)
                .activation(Activation.SIGMOID)
                .build());

        // Capa de salida con función de pérdida XENT
        listBuilder.layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                .nIn(2)
                .nOut(1)
                .activation(Activation.SIGMOID)
                .build());

        MultiLayerNetwork model = new MultiLayerNetwork(listBuilder.build());
        model.init();
        return model;
    }

    /**
     * Con aprendizaje
     *
     * @return String
     */
    @GetMapping("/train")
    public String trainModel() {
        MultiLayerNetwork model = createModel();

        // aprendo mi modelo XOR jajajaja
        INDArray input = Nd4j.create(new double[][]{
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        });

        INDArray labels = Nd4j.create(new double[][]{
                {0},
                {1},
                {1},
                {0}
        });

        model.fit(input, labels);

        /*for (int i = 0; i < 10000; i++) {
            model.fit(input, labels);
        }*/

        INDArray output = model.output(input);
        return "Output after training:\n" + output.toString();
    }

    /**
     * Sin aprendizaje
     *
     * @param x1 double
     * @param x2 double
     * @return String
     */
    @GetMapping("/predict")
    public String predict(@RequestParam double x1, @RequestParam double x2) {

        MultiLayerNetwork model = createModel();

        // Hay que aprender el modelo antes o cargarlo desde el archivo

        INDArray input = Nd4j.create(new double[][]{{x1, x2}});
        INDArray output = model.output(input);

        return "Input: [" + x1 + ", " + x2 + "], Output: " + output.toString();
    }
}

