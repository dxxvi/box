 ```
FROM ubuntu:26.04

#USER root
ENV DEBIAN_FRONTEND=noninteractive TZ=America/New_York

RUN apt-get update && \
    apt-get install -y --no-install-recommends sudo curl gpg libwayland-client0 libwayland-egl1 build-essential ca-certificates && \
    rm -rf /var/lib/apt/lists/*

RUN groupmod -g 1000 ubuntu || groupadd -g 1000 ubuntu && \
    usermod -u 1000 -g 1000 ubuntu || useradd -u 1000 -g 1000 -m -s /bin/bash ubuntu && \
    echo "ubuntu:ubuntu" | chpasswd && \
    usermod -aG sudo ubuntu && \
    mkdir -p /run/user/1000 && \
    chown ubuntu:ubuntu /run/user/1000

# Switch to the configured ubuntu user
USER ubuntu
WORKDIR /home/ubuntu

ENV PATH="/home/ubuntu/.cargo/bin:${PATH}"
RUN mkdir -p /home/ubuntu/.local/share && \
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --no-modify-path --default-toolchain stable && \
    echo 'alias antigravity="antigravity --no-sandbox --ozone-platform=wayland"' >> /home/ubuntu/.bashrc && \
    echo 'PATH="$HOME/.cargo/bin:/jdk/bin:/Antigravity-IDE:$PATH"' >> /home/ubuntu/.profile && \
    echo 'fc-cache -f -v' >> /home/ubuntu/.bashrc

CMD ["/bin/bash"]
```
