# Forensic Face Matching System

Bu proje, devlet kurumları için güvenli ve bilimsel olarak doğrulanabilir yüz eşleştirme sistemi geliştirmeyi amaçlamaktadır. Adli bilişim alanında kullanılmak üzere tasarlanmıştır.

## Özellikler

- **Mikroservis Mimarisi**: Spring Boot ve FastAPI ile geliştirilmiş
- **Yüz Tanıma**: InsightFace/ArcFace modelleri ile derin öğrenme
- **Güvenlik**: AES-256 şifreleme, JWT authentication, RBAC
- **Audit Logging**: Değiştirilemez audit kayıtları
- **Video İşleme**: Video frame çıkarma ve kalite skorlama
- **Toplu Analiz**: RabbitMQ ile queue sistemi
- **Monitoring**: Prometheus, Grafana, ELK stack
- **Web Arayüzü**: React ile modern kullanıcı arayüzü

## Mimari

### Mikroservisler
- **Gateway Service**: API Gateway (Port: 8080)
- **Auth Service**: Kimlik doğrulama ve yetkilendirme (Port: 8081)
- **Case Service**: Vaka yönetimi (Port: 8082)
- **Storage Service**: Dosya yönetimi ve şifreleme (Port: 8083)
- **Audit Service**: Audit logging (Port: 8084)
- **AI Service**: Yüz tanıma ve analiz (Port: 8000)
- **Frontend**: React web arayüzü (Port: 3000)

### Veritabanları
- **PostgreSQL**: Ana veri depolama
- **MongoDB**: Audit logları
- **MinIO**: Dosya depolama
- **RabbitMQ**: Message queue

### Monitoring
- **Prometheus**: Metrik toplama
- **Grafana**: Dashboard ve görselleştirme
- **ELK Stack**: Log analizi

## Kurulum

### Gereksinimler
- Docker ve Docker Compose
- Java 17+
- Node.js 18+
- Python 3.9+

### Geliştirme Ortamı
\`\`\`bash
# Repository'yi klonlayın
git clone <repository-url>
cd forensic-face-matching

# Geliştirme ortamını başlatın
./scripts/dev-setup.sh
\`\`\`

### Production Ortamı
\`\`\`bash
# Production ortamını kurun
./scripts/production-setup.sh
\`\`\`

## Kullanım

### API Endpoints

#### Authentication
- `POST /api/auth/signin` - Giriş yapma
- `POST /api/auth/signup` - Kayıt olma
- `POST /api/auth/refresh` - Token yenileme

#### Case Management
- `GET /api/cases` - Vaka listesi
- `POST /api/cases` - Yeni vaka oluşturma
- `GET /api/cases/{id}` - Vaka detayları
- `PUT /api/cases/{id}` - Vaka güncelleme

#### Face Analysis
- `POST /api/ai/detect-faces` - Yüz tespiti
- `POST /api/ai/compare-faces` - Yüz karşılaştırma
- `POST /api/ai/batch-analysis` - Toplu analiz

#### File Management
- `POST /api/storage/upload` - Dosya yükleme
- `GET /api/storage/download/{id}` - Dosya indirme
- `DELETE /api/storage/{id}` - Dosya silme

### Web Arayüzü
- **Ana Sayfa**: http://localhost:3000
- **Giriş**: http://localhost:3000/login
- **Dashboard**: http://localhost:3000/dashboard
- **Vaka Yönetimi**: http://localhost:3000/cases
- **Analiz**: http://localhost:3000/analysis

## Güvenlik

### Şifreleme
- **AES-256**: Dosya şifreleme
- **JWT**: Token tabanlı authentication
- **HTTPS**: TLS 1.3 şifreleme

### Audit Logging
- Tüm işlemler kaydedilir
- Değiştirilemez kayıtlar
- Chain-of-custody takibi

### Yetkilendirme
- Role-Based Access Control (RBAC)
- Admin, Moderator, User rolleri
- API endpoint koruması

## Monitoring

### Metrikler
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/admin123)

### Loglar
- **Kibana**: http://localhost:5601
- **Elasticsearch**: http://localhost:9200

## Geliştirme

### Test
\`\`\`bash
# Unit testler
mvn test

# Integration testler
mvn verify

# Frontend testler
cd frontend
npm test
\`\`\`

### API Dokümantasyonu
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## İletişim

Proje hakkında sorularınız için issue açabilirsiniz.
