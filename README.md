# keycloak-extension
This project is an Open Source extension with the intent of expending [Keycloak](https://www.keycloak.org/) functionalities. This project enables:
* Phone number authentication - Keycloak supports **username** and **email**  but this plugins allows authentication through **phone** attribute;


## Guidelines

In order to authenticate with phone number :
* Phone number must start with **+** and is set on **phone** attribute within **User**. 
* Phone number must only be set on **one** (1) **User** otherwise the **Users** won't be to authenticate with **phone number**

![ScreenShot](https://drive.google.com/uc?export=download&id=1AjiW5g2rUWWm6ZFPOCMZw0quM2LPTxzs)



## Building

Ensure you have JDK 8 (or newer), Maven 3.5.4 (or newer) and Git installed

    java -version
    mvn -version
    git --version

To build Keycloak Phone Services run:

    mvn clean package
    

## Getting started

### Deploy plugin

In order to use this plugin , Keycloak must be stopped and then copy **com.raitonbl.keycloak.services.phone**  into **KEYCLOAK_HOME/providers/** .

Start **Keycloak** , [More details](https://www.keycloak.org/documentation.html)

### Enable Phone Authentication

Authenticate into the **Keycloak Console Admin** using the Administrator account.

![ScreenShot](https://drive.google.com/uc?export=download&id=11MYon2YDsPNBThmlbI-rClkWdeyXnmJv)

Go to Authentication

![ScreenShot](https://drive.google.com/uc?export=download&id=1-hA_14rNQiiB8Rdu5ZEHbqq6Pif_iWnk)


On **Browser** Flow , Click the **Copy** button and rename it ( **web browser** in this getting started but can be **anything else**  )

![ScreenShot](https://drive.google.com/uc?export=download&id=1F4uwNkdodmhr-MY3gS87_QHBO2d94s06)

Click the **OK** button

![ScreenShot](https://drive.google.com/uc?export=download&id=1mQAWsirgufIhH8nHAPKOM6YjnTL3HHJE)


Click **Actions** Link , within the **row** where **Auth Type** is **Web Browser Forms** row , then click on **Add execution**

![ScreenShot](https://drive.google.com/uc?export=download&id=10JxMtFn6rBt4TkVE8XG_zAr3obSJh5EP)

![ScreenShot](https://drive.google.com/uc?export=download&id=1wvkfS4o64eyd_b96wbsUwFvmDi0xTz22)

Select **Username/Mobile Password Form** on **Provider** combox

![ScreenShot](https://drive.google.com/uc?export=download&id=1E2U2V_JWt_WD1gX4-X_GkaajV5qc3oHa)

Click **Save**

![ScreenShot](https://drive.google.com/uc?export=download&id=15sx37pNK1d5Wx0rzM4sBw4hULL_tCQM3)

Click on the **upper arrow** within **Username/Mobile Password Form**  row until the row is bellow **Username Password Form**

![ScreenShot](https://drive.google.com/uc?export=download&id=1vE-SuAbQ803x5-oWKRWj7-RNzFSm12ak)

Click on the **Actions** Link within **Username Password Form** row

![ScreenShot](https://drive.google.com/uc?export=download&id=1fys7RxVIglJZ1RHSDvfqYjB_uWcxl655)

Click **Delete**

![ScreenShot](https://drive.google.com/uc?export=download&id=1clyITXrgb9XtB2BogbL8ABiL8DY73a-W)


Cick on **Bindings** under **Authentication**

![ScreenShot](https://drive.google.com/uc?export=download&id=1OZMXiXn-sovBC4lmbp1o6tIgM9JVNwiZ)

Select **Web Browser** on **Browser Flow** combox , and then hit **Save**

![ScreenShot](https://drive.google.com/uc?export=download&id=1xK3JS6sedX7ZzT-LgFfaZbdt7eNmVKOY)
