package wooteco.auth.service;

import org.springframework.stereotype.Service;
import wooteco.auth.dao.MemberDao;
import wooteco.auth.domain.Member;
import wooteco.auth.domain.Token;
import wooteco.auth.infrastructure.JwtTokenProvider;
import wooteco.exception.badRequest.PasswordIncorrectException;
import wooteco.exception.unauthorized.LoginFailException;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDao memberDao;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberDao memberDao) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberDao = memberDao;
    }

    public Token login(String email, String password) {
        Member member = memberDao.findByEmail(email).orElseThrow(LoginFailException::new);
        if (member.invalidPassword(password)) {
            throw new PasswordIncorrectException();
        }
        return new Token(jwtTokenProvider.createToken(member.getId().toString()));
    }
}
