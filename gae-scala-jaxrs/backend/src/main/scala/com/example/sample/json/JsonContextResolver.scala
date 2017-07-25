package com.example.sample.json

import javax.ws.rs.core.MediaType
import javax.ws.rs.ext.{ContextResolver, Provider}
import javax.ws.rs.{Consumes, Produces}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * [2014年のBlog記事を参考](http://d.hatena.ne.jp/Kazuhira/20141101/1414838251)にしたものの、
  * ContextResolverが何者で、どうしてObjectMapperを返すと動くのか把握してない。
  *
  * [この辺を見てなるほど](https://stackoverflow.com/questions/32478159/jax-rs-contextresolvert-undestanding) と思ったりもするけど
  * ObjectMapperが選択されるのはまあわかったのだが、なぜObjectMapperを使えるのかがよーわからん。
  *
  * 例えばplay-jsonなんかのReads/Writesなんかを使うように書く方法とかもあるんだろうか…
  */
@Provider
@Consumes(Array(MediaType.APPLICATION_JSON))
@Produces(Array(MediaType.APPLICATION_JSON))
class JsonContextResolver extends ContextResolver[ObjectMapper] {

  private val mapper = {
    val _mapper = new ObjectMapper()
    _mapper.registerModule(DefaultScalaModule)
    _mapper
  }

  override def getContext(`type`: Class[_]): ObjectMapper = {
    mapper
  }
}
