<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">

        <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="code" class="${properties.kcLabelClass!}">OTP PIN</label>
                </div>

                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="code" name="code" class="${properties.kcInputClass!}" autofocus value=""/>
                </div>

            </div>

            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">

                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <button data-sitekey="${siteKey}"  data-callback='setToken'  data-action='submit' class="g-recaptcha ${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}">${msg("doSubmit")}</button>
                </div>

                <input type="hidden" id="captcha" name="captcha" class="${properties.kcInputClass!}" autofocus
                       value=""/>

            </div>

        </form>

        <#if siteKey?? >
            <script src="https://www.google.com/recaptcha/api.js" nonce="{NONCE}"></script>
            <script>
                function setToken(token) {
                    document.getElementById('captcha').value = token;
                    document.getElementById('kc-reset-password-form').submit();
                }
            </script>
        </#if>

    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>

</@layout.registrationLayout>