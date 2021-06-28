package http;

/**
 * <ul>
 * 	<li>Structura de date de tip enumeratie <strong>enum</strong>, care expune toate metodele <strong>HTTP</strong>.</li>
 * 	<ul>
 * 	 <li>GET</li>
 * 	 <li>PUT</li>
 * 	 <li>PUSH</li>
 * 	 <li>DELETE</li>
 *  </ul>
 * </ul>
*/
public enum HTTPMethod{
    /**
     * GET
     */
    HTTP_GET {
        @Override
        public String toString(){
            return "GET";
        }
    },
    /**
     * PUT
     */
    HTTP_PUT{
        @Override
        public String toString(){
            return "PUT";
        }
    },
    /**
     * POST
     */
    HTTP_POST{
        @Override
        public String toString(){
            return "POST";
        }
    },
    /**
     * DELETE
     */
    HTTP_DELETE{
        @Override
        public String toString() {
            return "DELETE";
        }
    };

}

