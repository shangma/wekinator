/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.io.Serializable;
import wekinator.FeatureConfiguration.Feature;

/**
 *
 * @author rebecca
 */
public class MetaFeature implements Serializable {

    protected Feature myFeature = null;
    protected String myName = "DefaultMetaFeature";

    public enum Type {

        DELTA_1s,
        DELTA_2,
        SMOOTH_1
    };

    public static String nameForType(Type type) {
        switch (type) {
            case DELTA_1s:
                return "1stDer";
            case DELTA_2:
                return "2ndDer";
            case SMOOTH_1:
                return "Smooth1";
        }
        return "Other";
    }

    public Type getType() {
        return null;
    }

    public static MetaFeature createForType(Type type, Feature f) {
        switch (type) {
            case DELTA_1s:
                return new Delta1(f);
            case DELTA_2:
                return new Delta2(f);
            case SMOOTH_1:
                return new Smooth1(f);
        }
        return null;

    }

    public String getOperationName() {
        return myName;
    }

    public String getFeatureName() {
        return getOperationName() + "_" + myFeature.name;
    }

    public Feature getFeature() {
        return myFeature;
    }

    public double[] computeForNextFeature(double[] f, int startIndex) {
        return f;

    }
}

class Delta1 extends MetaFeature {

    double last = 0.0;

    protected Delta1(Feature f) {
        this.myFeature = f;
        this.myName = MetaFeature.nameForType(Type.DELTA_1s);
    }

    @Override
    public Type getType() {
        return Type.DELTA_1s;
    }

    @Override
    public double[] computeForNextFeature(double[] f, int startIndex) {
        double[] val = new double[1];
        val[0] = f[startIndex] - last;
        last = f[startIndex];
        return val;
    }
}

class Delta2 extends MetaFeature {

    double lastValue = 0.0;
    double lastDiff = 0.0;

    public double[] computeForNextFeature(double[] f, int startIndex) {
        double[] val = new double[1];
        double thisDiff = f[startIndex] - lastValue;
        lastValue = f[startIndex];
        val[0] = thisDiff - lastDiff;
        lastDiff = thisDiff;
        return val;
    }

    protected Delta2(Feature f) {
        this.myFeature = f;
        this.myName = MetaFeature.nameForType(Type.DELTA_2);
    }

    @Override
    public Type getType() {
        return Type.DELTA_2;
    }
}

class Smooth1 extends MetaFeature {

    double last = 0.0;

    protected Smooth1(Feature f) {
        this.myFeature = f;
        this.myName = MetaFeature.nameForType(Type.SMOOTH_1);
    }

    @Override
    public Type getType() {
        return Type.SMOOTH_1;
    }

    @Override
    public double[] computeForNextFeature(double[] f, int startIndex) {
        double[] val = new double[1];
        val[0] = (f[startIndex] + last) * .5;
        last = f[startIndex];
        return val;
    }
}


