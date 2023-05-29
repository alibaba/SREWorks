ALTER TABLE `sreworks_stream_job`
    ADD Unique KEY `name`(`name`)
;

ALTER TABLE `sreworks_stream_job_block`
    ADD UNIQUE KEY `stream_job_index` (`stream_job_id`,`name`,`block_type`)
;

ALTER TABLE `sreworks_stream_job_block`
    ADD KEY `stream_job_id` (`stream_job_id`)
;