package logger;

/**
 * <ul>
 * 	<li>Enum care contine tipurile evenimentelor din sistem.</li>
 * </ul>
 */
public enum LogMsgType{
    /**
     * Eveniment realizat cu succes.
     */
    SUCCESS{
        @Override
        public String toString() {
            return "SUCCESS";
        }
    },
    /**
     * Eveniment care a generat o atentionare.
     */
    WARNING{
        @Override
        public String toString() {
            return "WARNING";
        }
    },
    /**
     * Eveniment care a generat o eroare.
     */
    ERROR{
        @Override
        public String toString() {
            return "ERROR";
        }
    }
}