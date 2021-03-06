/**
 MIT License

 Copyright (c) 2018 White Wood City

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package cn.net.polyglot

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.Launcher
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

Vertx vertx = vertx

JsonObject config = new JsonObject()
  .put("version",0.1d)
  .put("dir", new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + File.separator + "social-vertex")
  .put("host", "localhost")
  .put("tcp-port", 7373)
  .put("http-port",80)

ConfigStoreOptions fileStore = new ConfigStoreOptions()
  .setType("file")
  .setConfig(new JsonObject().put("path", "config.json"))

ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore)

ConfigRetriever retriever = ConfigRetriever.create(vertx, options)

retriever.getConfig { ar ->

  try {

    if (ar.succeeded()) {
      config.mergeIn(JsonObject.mapFrom(ar.result()))
    } else {
      System.out.println("The configuration file: config.json does not exist or in wrong format, use default config.")
    }

    retriever.close()

    if (!vertx.fileSystem().existsBlocking(config.getString("dir"))) {
      vertx.fileSystem().mkdirBlocking(config.getString("dir"))
    }

    def option = new DeploymentOptions().setConfig(config)
    vertx.deployVerticle("kt:cn.net.polyglot.verticle.WebServerVerticle", option)
    vertx.deployVerticle("kt:cn.net.polyglot.verticle.IMTcpServerVerticle", option)
    vertx.deployVerticle("kt:cn.net.polyglot.verticle.IMMessageVerticle", option)

  } catch (Exception e) {
    e.printStackTrace()
  }

}
