pragma solidity ^0.4.16;

import "./Token.sol";

contract owned {
    address public owner;

    function owned () public {
        owner = msg.sender;
    }

    modifier ownerOnly {
        require (owner == msg.sender);
        _;
    }

    function transferOwnership(address newOwner) ownerOnly public {
        owner = newOwner;
    }
}

contract TstToken is owned, TokenERC20 {

    uint256 public buyPrice;
    event Minted(uint amount);

    /* Initializes contract with initial supply tokens to the creator of the contract */
    function TstToken(
        uint256 initialSupply,
        string tokenName,
        string tokenSymbol
    ) TokenERC20(initialSupply, tokenName, tokenSymbol) public {}

    
    function buy() payable public returns (uint amount) {
        amount = msg.value / buyPrice;                    // calculates the amount
        require(balanceOf[this] >= amount);               // checks if it has enough to sell
        balanceOf[msg.sender] += amount;                  // adds the amount to buyer's balance
        Transfer(this, msg.sender, amount);               // execute an event reflecting the change
        Minted(amount);
        return amount;                                    // ends function and returns
    }

    /// @notice Allow users to buy tokens for `newBuyPrice` eth
    /// @param newBuyPrice Price users can buy from the contract
    function setPrice(uint256 newBuyPrice) ownerOnly public {
        buyPrice = newBuyPrice;
    }
}