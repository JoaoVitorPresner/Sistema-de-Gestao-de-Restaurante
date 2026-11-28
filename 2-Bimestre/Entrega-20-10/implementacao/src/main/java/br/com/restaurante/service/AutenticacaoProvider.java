package br.com.restaurante.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AutenticacaoProvider extends DaoAuthenticationProvider {

    private final TentativasLoginService tentativas;

    public AutenticacaoProvider(
            UsuarioDetailsService usuarios,
            PasswordEncoder passwordEncoder,
            TentativasLoginService tentativas) {

        this.tentativas = tentativas;

        setUserDetailsService(usuarios);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String login = authentication.getName();

        try {

            Authentication resultado =
                    super.authenticate(authentication);

            tentativas.registrarSucesso(login);

            return resultado;

        } catch (BadCredentialsException e) {

            tentativas.registrarFalha(login);

            throw e;
        }
    }
}