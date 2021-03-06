package com.test

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.Clients
import org.pac4j.core.config.{Config, ConfigFactory}
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.http4s.{DefaultHttpActionAdapter, Http4sCacheSessionStore, Http4sCookieSessionStore}
import org.pac4j.oauth.client.FacebookClient
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration

class DemoConfigFactory extends ConfigFactory {
  override def build(parameters: AnyRef*): Config = {
    val clients = new Clients("http://localhost:8080/callback",
      oidcClient(),
      saml2Client(),
      formClient(),
      facebookClient())

    val config = new Config(clients)
    //config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[_ <: CommonProfile]("ROLE_ADMIN"))
    //config.addAuthorizer("custom", new CustomAuthorizer)
    config.setHttpActionAdapter(DefaultHttpActionAdapter)  // <-- Render a nicer page
    config.setSessionStore(new Http4sCacheSessionStore())
    //config.setSessionStore(Http4sCookieSessionStore)
    config
  }

  def oidcClient(): OidcClient[OidcProfile, OidcConfiguration] = {
    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId("343992089165-sp0l1km383i8cbm2j5nn20kbk5dk8hor.apps.googleusercontent.com")
    oidcConfiguration.setSecret("uR3D8ej1kIRPbqAFaxIE3HWh")
    oidcConfiguration.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    oidcConfiguration.setUseNonce(true)
    oidcConfiguration.addCustomParam("prompt", "consent")
    val oidcClient = new OidcClient[OidcProfile, OidcConfiguration](oidcConfiguration)

    val authorizationGenerator = new AuthorizationGenerator[OidcProfile] {
      override def generate(context: WebContext, profile: OidcProfile): OidcProfile = {
        profile.addRole("ROLE_ADMIN")
        profile
      }
    }
    oidcClient.setAuthorizationGenerator(authorizationGenerator)
    oidcClient
  }

  def saml2Client() = {
    val cfg = new SAML2Configuration("file:saml-keystore.p12",
      "pac4j-demo-passwd",
      "pac4j-demo-passwd",
      "resource:metadata-okta.xml")
    cfg.setMaximumAuthenticationLifetime(3600)
    cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client")
    cfg.setServiceProviderMetadataPath("saml-sp-metadata.xml")
    new SAML2Client(cfg)
  }

  def formClient() =
    new FormClient("http://localhost:8080/loginForm", new SimpleTestUsernamePasswordAuthenticator())

  def facebookClient() =
    new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8")
}
