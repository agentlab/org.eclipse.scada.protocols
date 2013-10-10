/*******************************************************************************
 * Copyright (c) 2013 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.scada.protocol.modbus.message;

public class WriteMultiDataResponse extends BaseMessage
{
    private final int startAddress;

    private final int numRegisters;

    public WriteMultiDataResponse ( final byte unitIdentifier, final byte functionCode, final int startAddress, final int numRegisters )
    {
        super ( unitIdentifier, functionCode );
        this.startAddress = startAddress;
        this.numRegisters = numRegisters;
    }

    public int getNumRegisters ()
    {
        return this.numRegisters;
    }

    public int getStartAddress ()
    {
        return this.startAddress;
    }

}