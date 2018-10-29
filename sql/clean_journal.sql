delete from journal
where
    sequence_number < (
                      select max(sequence_number) as max_sequence_number
                      from public.snapshot where journal.persistence_id = snapshot.persistence_id group by persistence_id)
