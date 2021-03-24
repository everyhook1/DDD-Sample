/**
 * @(#)Job.java, 3月 24, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * @author liubin01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String machineId;
    private int sagaType;
    private int state;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTs;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTs;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTs;
}
