package com.edigest.journalApp.service;

import com.edigest.journalApp.entity.JournalEntry;
import com.edigest.journalApp.entity.User;
import com.edigest.journalApp.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class JournalEntryService {
    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName){
        try {
            // User in local memory
            User user = userService.findByUserName(userName);

            // Saving Entry to the database journal_entry DB
            journalEntry.setDate(LocalDateTime.now());
            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

            // Adding the entry to the user after saving in database
            user.getJournalEntries().add(savedEntry);

            // Saving the local user to the database after changes
            userService.saveUser(user);
        } catch (Exception ex){
            throw new RuntimeException("An error occurred while saving the entry "+ex);
        }
    }

    public void saveEntry(JournalEntry journalEntry){
        journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> getAll(){
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id){
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String userName){
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);

            // Deleting the journal entry associated with user using 'journal id'
            removed =user.getJournalEntries().removeIf(x -> x.getId().equals(id));

            if(removed){
                // Saving the user to database
                userService.saveUser(user);

                // deleting the journal entry from the database
                journalEntryRepository.deleteById(id);
            }
        } catch (Exception ex){
            log.error("Error: ",ex);
            throw new RuntimeException("An error occurred while removing the journal entry", ex);
        }
        return removed;
    }

    public List<JournalEntry> findByUserName(String userName){
        return null;
    }
}
