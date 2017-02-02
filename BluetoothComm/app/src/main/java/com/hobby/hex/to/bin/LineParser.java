/**
 * Copyright (c) 2016, Mallikarjun Tirlapur All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.hobby.hex.to.bin;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Class parses each line from the hex file to fetch address and data which are
 * then inserted into the hash table. Parsing is carried as per the intel
 * hexadecimal object file format specification.
 * 
 * @author Mallikarjun Tirlapur
 */

public class LineParser {

	public long StartAddress;

	/**
	 * Each line is processed and based on the type of the record, memory load address is
	 * determined.
	 * 
	 * @param lnTbl
	 *            hash table consisting of keys - line number and values -
	 *            string record
	 * @return linked hash table containing memory load address and bin data
	 */
	public LinkedHashMap<Long, String> processRecord(LinkedHashMap<Integer, String> lnTbl) {
		long upperBaseAddress = 0;
		LinkedHashMap<Long, String> addDataMap = new LinkedHashMap<Long, String>();
		Set<Integer> lnKey = lnTbl.keySet();
		for (Integer key : lnKey) {
			/*
			 * instance of record which holds current data in the record fields
			 */
			Record parsedLine = parseRecord(lnTbl.get(key));
			if (parsedLine != null) {
				switch (parsedLine.recTyp) {
				case RecordType.DataRecord:
					/*
					 * the data filed of DataRecord record specifies the binary
					 * data and load offset specifies memory load address
					 */
					addDataMap.put((upperBaseAddress | Long.parseLong(parsedLine.loadOffset, 16)),
							parsedLine.dataBytes);
					break;

				case RecordType.ExtendedLinearAddress:
					/*
					 * the data filed of ELA record specifies the upper 16 bits
					 * of the LBA
					 */
					upperBaseAddress = Long.parseLong(parsedLine.dataBytes, 16);
					upperBaseAddress <<= 16;
					break;

				case RecordType.ExtendedSegAddress:
					/*
					 * the data filed of ESA record specifies the bits 4 - 19 of
					 * the SBA
					 */
					upperBaseAddress = Long.parseLong(parsedLine.dataBytes, 16);
					upperBaseAddress <<= 4;
					break;

				case RecordType.StartLinearAddress:
					/*
					 * the data filed of SLA record specifies the execution
					 * start address
					 */
					StartAddress = Long.parseLong(parsedLine.dataBytes, 16);
					break;

				case RecordType.StartSegAddress:
					/*
					 * the data filed of SSA record specifies the the execution
					 * start address for the object file
					 */
					StartAddress = Long.parseLong(parsedLine.dataBytes, 16);
					break;

				case RecordType.EndOfFile:
					break;
				}

			} else {
				System.out.print("Checksum Failed!!!load valid hex file");
			}
		}
		/* linked hash table */
		return addDataMap;
	}

	/**
	 * Parses each record and updates the record fields of the class Record.
	 * 
	 * @param record
	 *            record is a line from the hex file which starts with ":"
	 * 
	 * @return instance of Record.
	 */
	public Record parseRecord(String record) {
		int sum = 0;
		Record lnRec = new Record();
		/* check if line starts with ':' or not */
		if (!Record.recMark.equals(record.substring(RecordsPosition.startCodePos, RecordsPosition.byteCountPos))) {
			return null;
		}
		/* parse for data length */
		lnRec.recLen = Integer.parseInt(record.substring(RecordsPosition.byteCountPos, RecordsPosition.addressPos), 16);
		/* parse for load offset */
		lnRec.loadOffset = record.substring(RecordsPosition.addressPos, RecordsPosition.recordTypePos);
		/* parse for record type */
		lnRec.recTyp = Integer.parseInt(record.substring(RecordsPosition.recordTypePos, RecordsPosition.dataPos), 16);
		/* parse for data bytes */
		lnRec.dataBytes = record.substring(RecordsPosition.dataPos, record.length() - 2);

		/* verify checksum */
		for (int i = 1; i < record.length(); i += 2) {
			sum += Integer.parseInt(record.substring(i, i + 2), 16);
			sum = sum & 0xff;
		}

		if (sum != 0) {
			/* return null if checksum is failed */
			return null;
		}
		/* return instance of Record */
		return lnRec;
	}
}
