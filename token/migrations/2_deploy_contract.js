var TstToken = artifacts.require("./TstToken.sol");

module.exports = function(deployer) {
  deployer.deploy(TstToken);
};