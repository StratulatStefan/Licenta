package logger;

public enum LogMsgType{
    SUCCESS{
        @Override
        public String toString() {
            return "SUCCESS";
        }
    },
    WARNING{
        @Override
        public String toString() {
            return "WARNING";
        }
    },
    ERROR{
        @Override
        public String toString() {
            return "ERROR";
        }
    }
}