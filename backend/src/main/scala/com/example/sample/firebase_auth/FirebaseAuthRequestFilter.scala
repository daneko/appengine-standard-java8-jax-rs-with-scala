package com.example.sample.firebase_auth

import java.security.Principal
import java.util.concurrent.ExecutionException
import java.util.logging.{Level, Logger}
import javax.annotation.Priority
import javax.ws.rs.container.{ContainerRequestContext, ContainerRequestFilter}
import javax.ws.rs.core.{HttpHeaders, SecurityContext}
import javax.ws.rs.ext.Provider
import javax.ws.rs.{NotAuthorizedException, Priorities}

import WithFirebaseAuth
import com.google.firebase.auth.{FirebaseAuth, FirebaseToken}
import com.google.firebase.tasks.Tasks

import scala.util.{Failure, Success, Try}

/**
  * Priorityは多分認可だよな？（FirebaseAuthのTokenをチェックするだけだし）
  */
@WithFirebaseAuth
@Provider
@Priority(Priorities.AUTHORIZATION)
class FirebaseAuthRequestFilter extends ContainerRequestFilter {

  val logger = Logger.getLogger(classOf[FirebaseAuthRequestFilter].getName)

  override def filter(requestContext: ContainerRequestContext): Unit = {
    logger.log(Level.INFO, "call filter")

    Option(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).
      filter(_.startsWith(FirebaseAuthRequestFilter.BearerPrefix)).
      map(_.substring(FirebaseAuthRequestFilter.Bearer.length).trim).
      fold[Try[String]](Failure(new NotAuthorizedException("Authorization header must be provided")))(Success(_)).
      flatMap(validationToken).
      fold[Unit]({
      case e: NotAuthorizedException => requestContext.abortWith(e.getResponse)
      case e => throw e
    }, token => setUserPrincipalName(requestContext, token.getUid))
  }

  def validationToken(token: String): Try[FirebaseToken] = {
    logger.log(Level.INFO, "call validation token")
    Try(Tasks.await(FirebaseAuth.getInstance().verifyIdToken(token))).transform(
      Success(_), {
        case e: ExecutionException => Failure(new NotAuthorizedException("error, token validate"))
        case e => Failure(new RuntimeException(e))
      }
    )
  }

  def setUserPrincipalName(requestContext: ContainerRequestContext, name: String): Unit = {
    val current = requestContext.getSecurityContext
    requestContext.setSecurityContext(new SecurityContext {
      override def getUserPrincipal: Principal = new Principal {
        override def getName: String = name
      }

      override def isSecure: Boolean = true

      override def getAuthenticationScheme: String = FirebaseAuthRequestFilter.Bearer

      override def isUserInRole(role: String): Boolean = current.isUserInRole(role)
    })
  }
}

object FirebaseAuthRequestFilter {
  val Bearer = "Bearer"
  val BearerPrefix = s"$Bearer "
}
