package com.example.sample.firebase_auth

import java.util.logging.{Level, Logger}
import javax.servlet.annotation.WebListener
import javax.servlet.{ServletContextEvent, ServletContextListener}

import com.google.firebase.auth.FirebaseCredentials
import com.google.firebase.{FirebaseApp, FirebaseOptions}

/**
  * firebaseの初期化のために用意
  * Documentが大変不親切（？）なのと、みんな古い情報使っているからか setServiceAccount を呼び出すようなものしか無い…
  * [migrationはここ](https://firebase.google.com/docs/admin/migrate-auth-50#use_setcredential_in_firebaseoptionsbuilder)
  *
  * そして[コンソール見てたらあった](https://console.firebase.google.com/u/1/project/todofirebaseproject-5a186/settings/serviceaccounts/adminsdk)
  *
  * [warm up としてdocumentがあるので、timeoutはこれで避けれる？](https://cloud.google.com/appengine/docs/standard/java/warmup-requests/configuring)
  *
  * 問題として、ここにdatabase url だとか key情報だとかを置いといて良いの？(特にjsonファイルをgit管理下に置く的な…)
  * これは単にサンプルなのでWEB-INF以下に配置している＆確認終わったら再発行するから良いけど…
  */
@WebListener
class FirebaseAppInitServletContextListener extends ServletContextListener {
  val logger = Logger.getLogger(classOf[FirebaseAppInitServletContextListener].getName)

  override def contextDestroyed(sce: ServletContextEvent): Unit = {
    logger.log(Level.INFO, "contextDestroyed")
  }

  override def contextInitialized(sce: ServletContextEvent): Unit = {
    logger.log(Level.INFO, "contextInitialized")

    val credential = FirebaseCredentials.
      fromCertificate(sce.getServletContext.getResourceAsStream("/WEB-INF/firebase-admin-sdk-key.json"))
    val options =
      new FirebaseOptions.Builder().
        setCredential(credential).
        setDatabaseUrl("https://todofirebaseproject-5a186.firebaseio.com").
        build()
    FirebaseApp.initializeApp(options)
  }
}
