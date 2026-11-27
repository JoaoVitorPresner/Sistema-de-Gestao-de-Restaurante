package br.com.restaurante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import br.com.restaurante.repository.UsuarioRepository;
import br.com.restaurante.security.ValidacaoSessaoFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UsuarioRepository usuarios) throws Exception {

        http

            .csrf(csrf -> csrf
                .csrfTokenRepository(
                    CookieCsrfTokenRepository
                        .withHttpOnlyFalse()
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Recursos publicos

                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()

                // Token CSRF

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/csrf"
                ).permitAll()

                // Login do cliente

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/cliente/login"
                ).permitAll()

                // Area exclusiva do cliente

                .requestMatchers(
                    "/api/cliente/**",
                    "/cardapio.html",
                    "/carrinho.html",
                    "/meus-pedidos.html"
                ).hasRole("CLIENTE")

                // Identificacao e painel dos funcionarios

                .requestMatchers(
                    "/api/auth/me",
                    "/dashboard.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE",
                    "ATENDENTE",
                    "COZINHEIRO",
                    "ESTOQUISTA"
                )

                // Produtos e categorias

                .requestMatchers(
                    "/api/categorias/**",
                    "/api/produtos/**",
                    "/produtos.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE"
                )

                // Gerenciamento de usuarios

                .requestMatchers(
                    "/api/usuarios/**",
                    "/usuarios.html"
                ).hasRole("ADMINISTRADOR")

                // Composicao dos produtos

                .requestMatchers(
                    "/api/estoque/composicoes/**"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE"
                )

                // Gerenciamento de estoque

                .requestMatchers(
                    "/api/estoque/**",
                    "/estoque.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE",
                    "ESTOQUISTA"
                )

                // Cozinha

                .requestMatchers(
                    "/api/cozinha/**",
                    "/cozinha.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE",
                    "COZINHEIRO"
                )

                // Gerenciamento de pedidos

                .requestMatchers(
                    "/api/pedidos",
                    "/api/pedidos/**",
                    "/pedidos.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE",
                    "ATENDENTE"
                )

                // Cadastro de mesas

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/mesas",
                    "/api/mesas/**"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE"
                )

                // Edicao de mesas

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/mesas/**"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE"
                )

                // Ativacao e desativacao de mesas

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/mesas/*/ativar",
                    "/api/mesas/*/desativar"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE"
                )

                // Estorno financeiro - RN03

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/pagamentos/contas/*/estornar"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE"
                )

                // Atendimento e gerenciamento de mesas

                .requestMatchers(
                    "/api/mesas/**",
                    "/mesas.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE",
                    "ATENDENTE"
                )

                // Consultas e finalizacao de pagamentos

                .requestMatchers(
                    "/api/pagamentos/**",
                    "/pagamentos.html"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "GERENTE",
                    "ATENDENTE"
                )

                // Bloqueia recursos nao autorizados

                .anyRequest().denyAll()
            )

            .formLogin(form -> form
                .loginPage("/index.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl(
                    "/dashboard.html",
                    true
                )
                .failureUrl("/index.html?erro")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index.html?saiu")
                .invalidateHttpSession(true)
                .deleteCookies(
                    "JSESSIONID",
                    "XSRF-TOKEN"
                )
                .permitAll()
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
            );

        http.addFilterBefore(
                new ValidacaoSessaoFilter(usuarios),
                AuthorizationFilter.class
        );

        return http.build();
    }
}