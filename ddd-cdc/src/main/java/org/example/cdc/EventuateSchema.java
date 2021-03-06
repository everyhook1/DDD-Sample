/**
 * @(#)EventuateSchema.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class EventuateSchema {
    private String eventuateDatabaseSchema;

    public String getEventuateDatabaseSchema() {
        return eventuateDatabaseSchema;
    }

    public void setEventuateDatabaseSchema(String eventuateDatabaseSchema) {
        this.eventuateDatabaseSchema = eventuateDatabaseSchema;
    }

    public boolean isEmpty() {
        return false;
    }
}
