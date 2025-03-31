package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayStackTransactionResponse {
    private boolean status;
    private String message;
    private Data data;
    private int StatusCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Data {
        /**
         * this is the redirect url that the user would use to make the payment
         */
        private String authorization_url;
        /**
         * this code identifies the payment url
         */
        private String access_code;
        /**
         * the unique reference used to identify this transaction
         */
        private String reference;


        public String getAuthorization_url() {
            return authorization_url;
        }

        public void setAuthorization_url(String authorization_url) {
            this.authorization_url = authorization_url;
        }

        public String getAccess_code() {
            return access_code;
        }
        public void setAccess_code(String access_code) {
            this.access_code = access_code;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }
    }

}
