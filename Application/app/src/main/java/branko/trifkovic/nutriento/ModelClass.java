package branko.trifkovic.nutriento;

public class ModelClass {

    private int mId;
    private String mDescription;
    private double mCalories;
    private double mProteins;
    private double mCarbs;
    private double mFats;

    public ModelClass(int mId, String mDescription, double mCalories, double mProteins, double mCarbs, double mFats) {
        this.mId = mId;
        this.mDescription = mDescription;
        this.mCalories = mCalories;
        this.mProteins = mProteins;
        this.mCarbs = mCarbs;
        this.mFats = mFats;
    }

    public String getmDescription() {
        return mDescription;
    }

    public int getmId() {
        return mId;
    }

    public double getmCalories() {
        return mCalories;
    }

    public double getmProteins() {
        return mProteins;
    }

    public double getmCarbs() {
        return mCarbs;
    }

    public double getmFats() {
        return mFats;
    }
}
