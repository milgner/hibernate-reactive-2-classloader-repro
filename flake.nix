{
  description = "A Nix-flake-based Kotlin development environment";

  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "github:nixos/nixpkgs";
  };

  outputs = { self , flake-utils , nixpkgs }:

  flake-utils.lib.eachDefaultSystem (system:
  let
    # 17 is LTS
    javaVersion = 17;

    overlays = [
      (self: super: rec {
        jdk = pkgs."jdk${toString javaVersion}";
        gradle = super.gradle.override {
          java = jdk;
        };
        kotlin = super.kotlin.override {
          jre = jdk;
        };
      })
    ];

    pkgs = import nixpkgs { inherit overlays system; };
  in
  {
    devShells.default = pkgs.mkShell {
      packages = with pkgs; [
        kotlin
      ];

      shellHook = ''
          ${pkgs.kotlin}/bin/kotlin -version
      '';
    };
  });
}
