package co.insou.spigsh;

public enum Try {

    ;

    @FunctionalInterface
    public interface CheckedRunnable
    {
        void run() throws Throwable;
    }

    public static void to(CheckedRunnable runnable)
    {
        try
        {
            runnable.run();
        }
        catch (Throwable throwable)
        {
            throw (throwable instanceof RuntimeException) ? (RuntimeException) throwable : new RuntimeException(throwable);
        }
    }

}
