package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MotoRepository {

    private final JdbcTemplate jdbc;

    public MotoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Moto> ROW_MAPPER = (rs, rowNum) -> {
        Moto m = new Moto();
        m.setId(rs.getLong("id"));
        m.setMotoNome(rs.getString("motoNome"));
        m.setMotoCor(rs.getString("motoCor"));
        m.setMotoAno(rs.getInt("motoAno"));
        m.setMotoValor(rs.getFloat("motoValor"));
        m.setMotoImagem(rs.getString("motoImagem"));
        m.setMotoAnunciadaEm(rs.getTimestamp("motoAnunciadaEm").toLocalDateTime());
        m.setMotoAtualizadaEm(rs.getTimestamp("motoAtualizadaEm").toLocalDateTime());
        m.setLoginId(rs.getLong("login_id"));
        return m;
    };

    public List<Moto> findAll() {
        return jdbc.query("SELECT * FROM moto ORDER BY id", ROW_MAPPER);
    }

    public Optional<Moto> findById(Long id) {
        return jdbc.query("SELECT * FROM moto WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<Moto> findTop3ByOrderByIdDesc() {
        return jdbc.query("SELECT * FROM moto ORDER BY id DESC LIMIT 3", ROW_MAPPER);
    }

    public List<Moto> findByLoginId(Long loginId) {
        return jdbc.query("SELECT * FROM moto WHERE login_id = ?", ROW_MAPPER, loginId);
    }

    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM moto WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public Moto save(Moto moto) {
        return moto.getId() == null ? insert(moto) : update(moto);
    }

    private Moto insert(Moto moto) {
        LocalDateTime agora = LocalDateTime.now();
        moto.setMotoAnunciadaEm(agora);
        moto.setMotoAtualizadaEm(agora);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO moto (motoNome, motoCor, motoAno, motoValor, motoImagem, motoAnunciadaEm, motoAtualizadaEm, login_id) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, moto.getMotoNome());
            ps.setString(2, moto.getMotoCor());
            ps.setInt(3, moto.getMotoAno());
            ps.setFloat(4, moto.getMotoValor());
            ps.setString(5, moto.getMotoImagem());
            ps.setTimestamp(6, Timestamp.valueOf(moto.getMotoAnunciadaEm()));
            ps.setTimestamp(7, Timestamp.valueOf(moto.getMotoAtualizadaEm()));
            ps.setLong(8, moto.getLoginId());
            return ps;
        }, keyHolder);

        moto.setId(keyHolder.getKey().longValue());
        return moto;
    }

    private Moto update(Moto moto) {
        moto.setMotoAtualizadaEm(LocalDateTime.now());
        jdbc.update("UPDATE moto SET motoNome=?, motoCor=?, motoAno=?, motoValor=?, motoImagem=?, motoAtualizadaEm=? WHERE id=?",
                moto.getMotoNome(), moto.getMotoCor(), moto.getMotoAno(), moto.getMotoValor(),
                moto.getMotoImagem(), Timestamp.valueOf(moto.getMotoAtualizadaEm()), moto.getId());
        return moto;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM moto WHERE id = ?", id);
    }
}