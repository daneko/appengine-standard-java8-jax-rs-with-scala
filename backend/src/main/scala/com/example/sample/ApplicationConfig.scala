package com.example.sample

import javax.ws.rs.ApplicationPath

import org.glassfish.jersey.server.ResourceConfig

/**
  */
@ApplicationPath("")
class ApplicationConfig extends ResourceConfig {
  packages(this.getClass.getPackage.getName)
}