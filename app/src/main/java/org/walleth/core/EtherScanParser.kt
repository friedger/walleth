package org.walleth.core

import org.json.JSONArray
import org.kethereum.model.Address
import org.kethereum.model.ChainDefinition
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.data.transactions.TransactionEntity
import org.walleth.data.transactions.TransactionSource
import org.walleth.data.transactions.TransactionState

import org.walleth.khex.hexToByteArray
import org.walleth.khex.toHexString
import java.math.BigInteger

fun parseEtherScanTransactions(jsonArray: JSONArray, chain: ChainDefinition): List<TransactionEntity> {
    return (0 until jsonArray.length()).map {
        val transactionJson = jsonArray.getJSONObject(it)
        val value = BigInteger(transactionJson.getString("value"))
        val timeStamp = transactionJson.getString("timeStamp").toLong()
        val input = transactionJson.getString("input").hexToByteArray()
        TransactionEntity(
                transactionJson.getString("hash"),
                createTransactionWithDefaults(
                        chain = chain,
                        value = value,
                        from = Address(transactionJson.getString("from")),
                        to = Address(transactionJson.getString("to")),
                        nonce = try {
                            BigInteger(transactionJson.getString("nonce"))
                        } catch (e: NumberFormatException) {
                            null
                        },
                        input = input.toList(),
                        txHash = transactionJson.getString("hash"),
                        creationEpochSecond = timeStamp
                ),
                signatureData = null,
                transactionState = TransactionState(false, isPending = false, source = TransactionSource.ETHERSCAN),
                transferData = parseTransferData(input)
        )
    }
}

data class TransferData(var tokenTo: Address, var tokenValue: BigInteger)

val transferMethodName = "0xa9059cbb".hexToByteArray()

fun parseTransferData(input: ByteArray): TransferData {
    if (input.size == 68 && isTransferFunction(input)) {
        val address = input.copyOfRange(4, 36).dropWhile { it == 0.toByte() }
        val value = input.copyOfRange(36, 68)
        return TransferData(Address(address.toHexString()), BigInteger(value))
    } else {
        return TransferData(Address("0x0"), BigInteger.valueOf(0))
    }
}

fun isTransferFunction(input: ByteArray) = input.copyOfRange(0, 4).contentEquals(transferMethodName)

