# T018c: use the 16 free cores

16 physical cores have been idle since about 20:30. Start **high_trap_G0/it0 image_v2** now as a separate background job, with 16 workers, `--scenarios configs/imaging/v2.yaml`, output `results/T018c/high_trap_G0/it0/image_v2/`. Remove that one command from the T018c_post_it0_01 queue, or let the queue skip outputs that already exist, so nothing is computed twice. The queue keeps the rest. Whenever 16 cores are free in future, start the next queued T018c item in parallel the same way; T018c comes before T016i.
