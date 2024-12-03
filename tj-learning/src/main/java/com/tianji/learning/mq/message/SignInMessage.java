package com.tianji.learning.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Encounter
 * @date 2024/12/02 22:29<br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SignInMessage
    {
        private Long userId;
        private Integer points;
    }
