package cz.monetplus.mips.eapi.v19.connector.entity.actions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import cz.monetplus.mips.eapi.v19.connector.entity.SignBase;
import cz.monetplus.mips.eapi.v19.connector.entity.responses.Endpoint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public
class AuthInit extends Action {
    private Endpoint browserInit;
    private SdkInit sdkInit;

    @Override
    public String toSign() {
        StringBuilder sb = new StringBuilder();
        if (null != browserInit) add(sb, browserInit.toSign());
        if (null != sdkInit) add(sb, sdkInit.toSign());
        deleteLast(sb);
        return sb.toString();
    }

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class SdkInit extends SignBase {
        private String directoryServerID;
        private String schemeId;
        private String messageVersion;

        @Override
        public String toSign() {
            StringBuilder sb = new StringBuilder();
            add(sb, directoryServerID);
            add(sb, schemeId);
            add(sb, messageVersion);
            deleteLast(sb);
            return sb.toString();
        }
    }
}
