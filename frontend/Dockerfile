# ----- 1. Aşama: Bağımlılıkları Yükleme ve Projeyi Derleme (Build) -----
FROM node:18-alpine AS builder

# pnpm paket yöneticisini yüklüyoruz
RUN npm install -g pnpm

WORKDIR /app

# Sadece paket tanım dosyalarını kopyalayıp bağımlılıkları yüklüyoruz
COPY package.json ./
RUN pnpm install

# Tüm proje kodunu kopyalıyoruz
COPY . .

# docker-compose.yml'den gelen build-time argümanını alıyoruz
ARG NEXT_PUBLIC_API_URL  # <-- BU SATIRI DEĞİŞTİRİN
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL} 

# Next.js projesini derliyoruz
RUN pnpm build


# ----- 2. Aşama: Üretim (Production) Imajını Oluşturma -----
FROM node:18-alpine AS runner

# ... (dosyanın geri kalanı aynı kalabilir) ...

WORKDIR /app

# Güvenlik için 'nextjs' adında, root olmayan bir kullanıcı oluşturuyoruz
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001

# Sadece production için gerekli dosyaları 'builder' aşamasından kopyalıyoruz
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.mjs ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json

# Dosya sahipliğini yeni kullanıcımıza veriyoruz
RUN chown -R nextjs:nodejs /app

# Yeni kullanıcıya geçiş yapıyoruz
USER nextjs

# Docker-compose.yml'nin beklediği 80 portunu açıyoruz
EXPOSE 80

# Next.js'in tüm IP'lere bağlanmasını sağlıyoruz
ENV HOST 0.0.0.0

# "pnpm start" komutu "next start" script'ini çalıştırır
# Uygulamayı 80 portundan başlatıyoruz
CMD ["next", "start", "-p", "80"]