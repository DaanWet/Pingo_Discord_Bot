package me.damascus2000.pingo.utils;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.log4j.MDC;

public class PieceWiseFunction implements UnivariateFunction {

    private final int a;
    private final double b;
    private final double c;
    private final int d;

    public PieceWiseFunction(int a, double b, double c, int d){
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }


    @Override
    public double value(double v){
        if (v < d){
            return a * Math.pow(b, v);
        } else {
            return c * (v - d) + a * Math.pow(b, d);
        }
    }

    public double integrate(double x){
        if (x <= 0)
            return 0;
        SimpsonIntegrator integrator = new SimpsonIntegrator();
        return integrator.integrate(10000, this, 0, x);
    }

    public double solveForX(double y){
        MDC.put("y", y);
        BrentSolver solver = new BrentSolver();
        UnivariateFunction function = f -> integrate(f) - y;
        return solver.solve(10000, function, 0, 1000);
    }

}
