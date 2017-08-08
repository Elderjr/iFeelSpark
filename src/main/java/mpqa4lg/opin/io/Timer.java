package mpqa4lg.opin.io;

public class Timer
{
    double t;
    
    public Timer() {
        this.reset();
    }
    
    public void reset() {
        this.t = System.currentTimeMillis();
    }
    
    public double elapsed() {
        return (System.currentTimeMillis() - this.t) / 1000.0;
    }
    
    public void print(final String s) {
        System.out.println(String.valueOf(s) + ": " + (int)Math.floor(this.elapsed() / 60.0) + "min " + (int)Math.round(this.elapsed() % 60.0) + "sec");
    }
    
    public void printMem(final String s) {
        System.out.println(String.valueOf(s) + ": " + (int)Math.floor(this.elapsed() / 60.0) + "min " + (int)Math.round(this.elapsed() % 60.0) + "sec");
        final Runtime r = Runtime.getRuntime();
        System.out.println("Used Memory : " + (r.totalMemory() - r.freeMemory()) / 1048576L);
        System.out.println("Max Memory : " + r.maxMemory() / 1048576L);
    }
}
