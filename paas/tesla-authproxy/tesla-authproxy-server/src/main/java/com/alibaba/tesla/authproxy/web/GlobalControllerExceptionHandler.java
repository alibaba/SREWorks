package com.alibaba.tesla.authproxy.web;

import com.alibaba.tesla.authproxy.ApplicationException;
import com.alibaba.tesla.authproxy.AuthProperties;
import com.alibaba.tesla.authproxy.Constants;
import com.alibaba.tesla.authproxy.constants.ErrorCode;
import com.alibaba.tesla.authproxy.exceptions.ClientUserArgsException;
import com.alibaba.tesla.authproxy.exceptions.TeslaUserArgsException;
import com.alibaba.tesla.authproxy.exceptions.UnAuthorizedException;
import com.alibaba.tesla.authproxy.lib.exceptions.*;
import com.alibaba.tesla.authproxy.model.AppDO;
import com.alibaba.tesla.authproxy.service.TeslaAppService;
import com.alibaba.tesla.authproxy.service.impl.OamAuthServiceManager;
import com.alibaba.tesla.authproxy.util.CookieUtil;
import com.alibaba.tesla.authproxy.util.ResponseUtil;
import com.alibaba.tesla.authproxy.util.StringUtil;
import com.alibaba.tesla.authproxy.web.common.PrivateResultBuilder;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.common.base.TeslaResultFactory;
import com.alibaba.tesla.web.controller.BaseController;
import com.alibaba.tesla.common.utils.TeslaResult;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ?????????????????? Controller
 *
 * @author tandong.td@alibaba-inc.com yaoxing.gyx@alibaba-inc.com
 */
@RestController
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalControllerExceptionHandler extends BaseController {

    /**
     * ??????????????????????????? URL??????????????????????????????
     */
    private static final String SELF_LOGIN_URL = "SELF";

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private TeslaAppService teslaAppService;

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public void baseErrorHandler(HttpServletRequest req, HttpServletResponse res, Exception e) {
        log.error("Exception occured, host={}, url={}, error={}", req.getRemoteHost(), req.getRequestURL(),
            ExceptionUtils.getStackTrace(e));
        ResponseUtil.writeErrorJson(res, TeslaResult.FAILURE, e.getMessage(), ExceptionUtils.getStackTrace(e));
    }

    @ExceptionHandler(value = UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public void unAuthorizedHandler() {
    }

    @ExceptionHandler(value = {AuthProxyException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TeslaBaseResult authProxyExceptionHandler(AuthProxyException ex) {
        AuthProxyErrorCode errorCode = ex.getErrorCode();
        if (AuthProxyErrorCode.INVALID_USER_ARGS.getCode() == errorCode.getCode()
            || AuthProxyErrorCode.USER_CONFIG_ERROR.getCode() == errorCode.getCode()
            || AuthProxyErrorCode.DUPLICATE_ENTRY.getCode() == errorCode.getCode()) {
            return TeslaResultFactory.buildClientErrorResult(ex.toString());
        }
        return TeslaResultFactory.buildResult(ErrorCode.SERVER_ERROR, ex.toString());
    }

    @ExceptionHandler(value = InvalidGrantException.class)
    @ResponseBody
    public TeslaBaseResult invalidGrantExceptionHandler(InvalidGrantException e) {
        return buildResult(ErrorCode.USER_ARG_ERROR, e.getOAuth2ErrorCode(), ErrorCode.EMPTY_OBJ);
    }

    @ExceptionHandler(value = {TeslaUserArgsException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    protected TeslaBaseResult teslaUserArgsException(TeslaUserArgsException e, HttpServletRequest request) {
        return TeslaResultFactory.buildResult(e.getErrCode(), e.getErrMessage(), e.getErrData());
    }

    @ExceptionHandler(value = ApplicationException.class)
    @ResponseBody
    public void defaultErrorHandler(
        HttpServletRequest request, HttpServletResponse response, ApplicationException exception) {
        log.warn("ApplicationException occured: {}", ExceptionUtils.getStackTrace(exception));
        // ??????????????????
        if (TeslaResult.NOAUTH == exception.getErrCode()) {
            String serverUrl = authProperties.getServerUrl();
            String loginUrl = authProperties.getLoginUrl();
            String refererUrl = request.getHeader(HttpHeaders.REFERER);
            if (null == refererUrl || refererUrl.length() == 0) {
                refererUrl = authProperties.getCallbackUrl();
            }
            String realCallbackUrl = null;
            try {
                if (OamAuthServiceManager.class.getTypeName().equals(authProperties.getAuthPolicy())) {
                    realCallbackUrl = loginUrl + "?oauth_callback=" + URLEncoder.encode(refererUrl, "UTF-8");
                } else {
                    realCallbackUrl = serverUrl + "/api-proxy/auth/login?backurl=" + URLEncoder.encode(refererUrl,
                        "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                log.error("Invalid callback url in system application properties, callback={}, msg={}",
                    refererUrl, e.getMessage());
                ResponseUtil.writeErrorJson(response, null);
            }
            Map<String, String> data = new HashMap<>();
            data.put("loginUrl", realCallbackUrl);
            ResponseUtil.writeErrorJson(response, TeslaResult.NOAUTH, data, exception.getMessage());
        } else {
            String errorMessage = exception.getMessage();
            if (null != exception.getCause()) {
                errorMessage = exception.getCause().getMessage();
            }
            ResponseUtil.writeErrorJson(
                response, TeslaResult.FAILURE, exception.getClass().getSimpleName(), errorMessage);
        }
    }

    /**
     * ?????????????????? ValidationError ??????
     */
    @ExceptionHandler(value = PrivateValidationError.class)
    @ResponseBody
    public TeslaResult privateValidationErrorHandler(PrivateValidationError e) {
        return PrivateResultBuilder.buildExtValidationErrorResult(e.getErrorMap());
    }

    @ExceptionHandler(value = DataBaseValidationError.class)
    @ResponseBody
    public TeslaBaseResult databaseValidationErrorHandler(DataBaseValidationError e) {
        return buildResult(TeslaResult.NOAUTH,  e.getErrorMessage(), null);
    }

    /**
     * ?????????????????? AuthProxyThirdPartyError ??????
     */
    @ExceptionHandler(value = AuthProxyThirdPartyError.class)
    @ResponseBody
    public TeslaResult privateThirdPartyErrorHandler(AuthProxyThirdPartyError e) {
        Map<String, String> info = new HashMap<String, String>() {{
            put("type", "AuthProxyThirdPartyError");
            put("component", e.getComponentName());
        }};
        return PrivateResultBuilder.buildExtBadRequestResult(e.getMessage(), info);
    }

    /**
     * ?????? ClientUserArgsException ??????
     */
    @ExceptionHandler(value = ClientUserArgsException.class)
    @ResponseBody
    public TeslaBaseResult clientUserArgsHandler(ClientUserArgsException e) {
        return buildResult(ErrorCode.USER_ARG_ERROR, e.getMessage(), ErrorCode.EMPTY_OBJ);
    }

    /**
     * ?????? HttpMessageNotReadableException ??????
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseBody
    public TeslaBaseResult httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        return buildResult(ErrorCode.USER_ARG_ERROR, e.getMessage(), ErrorCode.EMPTY_OBJ);
    }

    /**
     * ?????? PrivateAuthForbidden ??????
     */
    @ExceptionHandler(value = PrivateAuthForbidden.class)
    @ResponseBody
    public TeslaResult privateAuthForbiddenHandler(PrivateAuthForbidden e) {
        return PrivateResultBuilder.buildForbiddenResult(e.getMessage(), e.getResourcePath());
    }

    /**
     * ?????? PrivateAuthNotLogin ??????
     */
    @ExceptionHandler(value = PrivateAuthNotLogin.class)
    @ResponseBody
    public TeslaResult privateAuthNotLoginHandler(PrivateAuthNotLogin e, HttpServletRequest request,
        HttpServletResponse response) {
        CookieUtil.cleanLoginCookie(request, response, Constants.COOKIE_AAS_LOGIN_TICKET);
        CookieUtil.cleanLoginCookie(request, response, Constants.COOKIE_AAS_LOGIN_USER_ID);
        CookieUtil.cleanLoginCookie(request, response, Constants.COOKIE_AAS_LOGIN_ALIYUNID);
        return PrivateResultBuilder.buildNotLoginResult(e.getMessage(), getPrivateLoginUrl(request));
    }

    /**
     * ?????? AliyunAuthNotLogin ??????
     */
    @ExceptionHandler(value = AliyunAuthNotLogin.class)
    @ResponseBody
    public TeslaResult aliyunAuthNotLoginHandler(AliyunAuthNotLogin e, HttpServletRequest request,
        HttpServletResponse response) {
        String topDomain = CookieUtil.getCookieDomain(request, authProperties.getCookieDomain());
        CookieUtil.cleanDomainCookie(request, response, Constants.COOKIE_ALIYUN_ACCESS_TOKEN, topDomain, "/");
        CookieUtil.cleanDomainCookie(request, response, Constants.COOKIE_ALIYUN_REFRESH_TOKEN, topDomain, "/");
        CookieUtil.cleanDomainCookie(request, response, Constants.COOKIE_ALIYUN_EXPIRES_AT, topDomain, "/");
        return ResponseUtil.buildNotLoginResponse(e.getMessage(), getAliyunLoginUrl(request));
    }

    /**
     * ?????? PrivateAuthNotLogin ??????
     */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseBody
    public TeslaResult noHandlerFoundHandler() {
        return PrivateResultBuilder.buildNotFoundResult();
    }

    /**
     * ???????????????????????????(??????????????????????????????)
     */
    private String getAliyunLoginUrl(HttpServletRequest request) {
        String baseUrl = authProperties.getOauth2UserAuthorizationUri();
        HttpUrl.Builder queryUrl = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder();
        queryUrl.addQueryParameter("client_id", authProperties.getOauth2ClientId());
        queryUrl.addQueryParameter("redirect_uri", authProperties.getOauth2RedirectUri());
        queryUrl.addQueryParameter("response_type", "code");
        queryUrl.addQueryParameter("access_type", "offline");
        JsonObject stateJson = new JsonObject();
        String callbackUrl = request.getParameter("callback");
        if (StringUtil.isEmpty(callbackUrl)) {
            callbackUrl = request.getHeader(HttpHeaders.REFERER);
        }
        if (StringUtil.isEmpty(callbackUrl)) {
            callbackUrl = authProperties.getCallbackUrl();
        }
        stateJson.addProperty("callback", callbackUrl);
        queryUrl.addQueryParameter("state", stateJson.toString());
        return queryUrl.toString();
    }

    /**
     * ???????????????????????????(?????????????????????)
     */
    private String getPrivateLoginUrl(HttpServletRequest request) {
        // AAS ????????? callback ?????? key
        String aasCallbackKey = "?oauth_callback=";
        // ???????????????????????? callback ?????? key
        String customCallbackKey = "?callback=";
        String appId = request.getParameter("appId");
        String refererUrl = request.getHeader(HttpHeaders.REFERER);
        if (null == refererUrl || refererUrl.length() == 0) {
            refererUrl = authProperties.getCallbackUrl();
        }
        String callbackUrl = "";
        try {
            callbackUrl = URLEncoder.encode(refererUrl, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        // ??????????????? appId ???????????????????????????????????? loginUrl
        if (null == appId || appId.length() == 0) {
            return authProperties.getLoginUrl() + aasCallbackKey + callbackUrl;
        }
        AppDO appDo = teslaAppService.getByAppId(appId);
        // ??? app ???????????? loginUrl ?????????????????????????????? loginUrl
        if (null == appDo || null == appDo.getLoginUrl() || appDo.getLoginUrl().length() == 0) {
            return authProperties.getLoginUrl() + aasCallbackKey + callbackUrl;
        }
        // ??? app ????????? loginUrl ????????? AAS ??? loginUrl ???
        if (appDo.getLoginUrl().equals(authProperties.getLoginUrl())) {
            return appDo.getLoginUrl() + aasCallbackKey + callbackUrl;
        }
        // ??? app ????????? URL == SELF ???????????????????????????????????????????????????????????????????????????????????????
        if (appDo.getLoginUrl().equals(SELF_LOGIN_URL)) {
            return "";
        }
        // ??? app ??????????????????????????????
        return appDo.getLoginUrl() + customCallbackKey + callbackUrl;
    }
}