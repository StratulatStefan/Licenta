package http;

public enum HTTPMethod{
    HTTP_GET {
        @Override
        public String toString(){
            return "GET";
        }
    },
    HTTP_PUT{
        @Override
        public String toString(){
            return "PUT";
        }
    },
    HTTP_POST{
        @Override
        public String toString(){
            return "POST";
        }
    },
    HTTP_DELETE{
        @Override
        public String toString() {
            return "DELETE";
        }
    };

}

