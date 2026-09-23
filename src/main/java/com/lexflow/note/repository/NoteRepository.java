package com.lexflow.note.repository;

import com.lexflow.note.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByLegalCaseIdOrderByIdDesc(Long caseId);
}