package methods;

public enum Selection {
    FITNESS_PROPORTIONATE(),
    POWER(4),
    TOURNAMENT(5, 0.5);

    public int power;
    public int size;
    public double probability;

    Selection(final int size, final double probability) {
        this.size = size;
        this.probability = probability;
    }

    Selection() {
    }

    Selection(final int power) {
        this.power = power;
    }
}
