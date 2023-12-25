package branko.trifkovic.nutriento;

public class Softmax {
    private float[] values;

    public Softmax(float[] values) {
        this.values = values;
    }

    public float[] softmax() {
        float[] retValues = new float[values.length];
        float denominator = 0;

        for(int i = 0; i < values.length; i++) {
            denominator += Math.exp(values[i]);
        }

        for(int i = 0; i < values.length; i++) {
            retValues[i] = (float) Math.exp(values[i]) / denominator;
        }

        return retValues;
    }

}
