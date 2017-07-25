## Task Queueの動作確認

* LocalでModuleを越えた確認ができない
  * https://github.com/GoogleCloudPlatform/appengine-modules-sample-java/issues/18#issuecomment-188454284 とかはある
  * が https://github.com/GoogleCloudPlatform/appengine-modules-sample-java が現行ではないgradle pluginとmavenbaseなので…

* /WEB-INF 以下に queue.yaml よりも queue.xml の方が良い?
  * 結局ローカルで動作確認する場合、`http://localhost:8080/_ah/admin/taskqueue` に yamlだと反応しない

## Task Queueにjsonを渡す

* コードに書いた通りで動いた

