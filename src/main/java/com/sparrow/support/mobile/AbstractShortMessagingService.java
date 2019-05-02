package com.sparrow.support.mobile;

import com.sparrow.constant.CONFIG;
import com.sparrow.constant.CONFIG_KEY_LANGUAGE;
import com.sparrow.constant.SPARROW_ERROR;
import com.sparrow.protocol.BusinessException;
import com.sparrow.protocol.MobileShortMessaging;
import com.sparrow.utility.Config;
import com.sparrow.utility.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author: zh_harry@163.com
 * @date: 2019-04-06 12:28
 * @description:
 */
public abstract class AbstractShortMessagingService implements ShortMessageService {

    private Logger logger = LoggerFactory.getLogger(AbstractShortMessagingService.class);

    public MobileShortMessaging.Builder init(String mobile) {
        MobileShortMessaging.Builder builder = new MobileShortMessaging.Builder()
                .companyName(Config.getLanguageValue(CONFIG_KEY_LANGUAGE.MOBILE_COMPANY))
                .key(Config.getValue(CONFIG.MOBILE_KEY))
                .mobile(mobile)
                .templateId(Config.getValue(CONFIG.MOBILE_TEMPLATE_ID));
        return builder;
    }


    /**
     * 验证码是否有效
     *
     * @param sendTime send time
     * @param business show label
     * @return 验证码是否有效
     */
    public Boolean valid(Long sendTime, String business) throws BusinessException {
        //手机验证码有效时间
        int mobileValidateTokenAvailableTime = Config
                .getIntegerValue(CONFIG.MOBILE_VALIDATE_TOKEN_AVAILABLE_TIME);
        Long currentTime = System.currentTimeMillis();
        Long validTime = sendTime + mobileValidateTokenAvailableTime * 1000;
        if (currentTime > validTime) {
            throw new BusinessException(SPARROW_ERROR.USER_VALIDATE_TIME_OUT, business);
        }
        return true;
    }

    /**
     * 验证码是否正确
     *
     * @param validateCode   current validate code
     * @param shortMessaging short message entity
     * @return 是否验证成功
     */
    public Boolean validate(String validateCode, MobileShortMessaging shortMessaging) throws BusinessException {
        if (Config.getBooleanValue(CONFIG.DEBUG)) {
            return true;
        }
        if (StringUtility.isNullOrEmpty(validateCode)) {
            throw new BusinessException(SPARROW_ERROR.GLOBAL_PARAMETER_NULL, shortMessaging.getBusiness());
        }
        Boolean result = valid(shortMessaging.getSendTime(), shortMessaging.getBusiness());
        if (!result) {
            return result;
        }
        if (!validateCode.equals(shortMessaging.getValidateCode())) {
            throw new BusinessException(SPARROW_ERROR.GLOBAL_VALIDATE_CODE_ERROR, shortMessaging.getBusiness());
        }
        return true;
    }

    public MobileShortMessaging.Builder validateCode(MobileShortMessaging.Builder builder) {
        Random random = new Random();
        Integer code = 100000 + random.nextInt(89999);
        builder.validateCode(code.toString());
        builder.sendTime(System.currentTimeMillis());
        String content = String.format("#code#=%1$s&#company#=%2$s", code, builder.getCompanyName());
        builder.content(content);
        logger.debug(content);
        return builder;
    }
}