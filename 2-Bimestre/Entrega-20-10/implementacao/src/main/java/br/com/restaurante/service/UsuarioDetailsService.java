package br.com.restaurante.service;

import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {

        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        Usuario usuario = usuarios.findByLogin(login)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario nao encontrado"
                        ));

        return new UsuarioAutenticado(usuario);
    }
}