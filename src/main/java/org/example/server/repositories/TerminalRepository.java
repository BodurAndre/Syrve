package org.example.server.repositories;

import org.example.server.models.TerminalGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalRepository extends JpaRepository<TerminalGroup,Long> {

}
