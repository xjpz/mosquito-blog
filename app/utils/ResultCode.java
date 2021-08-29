package utils;

/**
 * Created by xjpz on 2021/8/10.
 */
public enum ResultCode {

    OK(200, "操作成功"),
    FAILURE(500, "业务异常"),
    SERVER_ERROR(500, "服务器内部错"),
    AUTH_FAIL(401, "Token已失效"),
    NOT_PERMISSION(403, "你没有权限操作"),

    INVALID_PARAM(400, "参数错误"),
    REQ_METHOD_GET_NOT_SUPPORT(400,"不支持GET请求"),
    REQ_METHOD_POST_NOT_SUPPORT(400,"不支持POST请求"),

    PARAM_USER_NAME_NULL(400,"用户名不能为空"),
    PARAM_USER_PASSWORD_NULL(400,"密码不能为空"),
    PARAM_USER_NAME_OR_PASSWORD_ERROR(400,"用户名或密码错误"),
    PARAM_USER_NOT_EXIST(400,"用户不存在"),

    PARAM_ID_NULL(400,"id不能为空"),
    PARAM_PHONE_NULL(400,"手机号不能为空"),
    PARAM_PHONE_ERROR(400,"手机号格式不正确"),

    PARAM_USER_ID_NULL(400,"用户id不能为空"),
    PARAM_INVITE_SEND_USER_ID_NULL(400,"邀请人id不能为空"),
    PARAM_INVITE_RECV_USER_ID_NULL(400,"被邀请人id不能为空"),

    PARAM_INVITE_ID_NULL(400,"邀请id不能为空"),
    PARAM_INVITE_STATUS_NULL(400,"邀请状态不能为空"),
    PARAM_GENDER_NULL(400,"性别不能为空"),
    PARAM_BIND_TYPE_ERROR(400,"绑定类型不能为空"),
    PARAM_OPENID_NULL(400,"openId不能为空"),


    ;


    private int code;

    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public boolean isOK() {
        return OK.code == code;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
