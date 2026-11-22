package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.TournamentRequest;
import com.cricarena.backend.dto.response.TournamentResponse;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TournamentMapper;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.service.TournamentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMapper tournamentMapper;

    public TournamentServiceImpl(TournamentRepository tournamentRepository, TournamentMapper tournamentMapper) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentMapper = tournamentMapper;
    }

    @Override
    public TournamentResponse createTournament(TournamentRequest request) {
        Tournament tournament = tournamentMapper.toEntity(request);
        Tournament savedTournament = tournamentRepository.save(tournament);
        return tournamentMapper.toResponse(savedTournament);
    }

    @Override
    public TournamentResponse getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
        return tournamentMapper.toResponse(tournament);
    }

    @Override
    public List<TournamentResponse> getAllTournaments() {
        return tournamentRepository.findAll()
                .stream()
                .map(tournamentMapper::toResponse)
                .toList();
    }

    @Override
    public TournamentResponse updateTournament(Long id, TournamentRequest request) {
        Tournament existingTournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));

        existingTournament.setName(request.getName());
        existingTournament.setDescription(request.getDescription());
        existingTournament.setFormat(request.getFormat());
        existingTournament.setStartDate(request.getStartDate());
        existingTournament.setEndDate(request.getEndDate());

        Tournament updatedTournament = tournamentRepository.save(existingTournament);
        return tournamentMapper.toResponse(updatedTournament);
    }

    @Override
    public void deleteTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
        tournamentRepository.delete(tournament);
    }
}
