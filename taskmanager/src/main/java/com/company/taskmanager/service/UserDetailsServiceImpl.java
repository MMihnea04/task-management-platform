package com.company.taskmanager.service;

import com.company.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Service, spune lui Spring ca aceasta clasa contine logica de business
// RequiredArgsConstructor, Lombok genereaza automat constructorul pt variabilele final
// asta inlocuieste @Autowired cu dependency injection
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    // UserDetailsService, interfata de cautare a user-ului

    // final, campul trebuie initializat prin constructor (injectat de Spring)
    private final UserRepository userRepository;

    // loadUserByUsername, metoda apelata de Spring Security la autentificare
    // primeste username-ul si trebuie sa returneze UserDetails
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // cautam userul in baza de date dupa username
        // daca nu exista, aruncam exceptie cu mesaj descriptiv
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Userul cu username-ul '" + username + "' nu a fost gasit"
                ));
    }
}