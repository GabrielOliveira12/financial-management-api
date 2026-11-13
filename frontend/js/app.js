const Auth = {
    TOKEN_KEY: "fm_token",
    USERNAME_KEY: "fm_username",

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    setSession(token, username) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USERNAME_KEY, username);
    },

    getUsername() {
        return localStorage.getItem(this.USERNAME_KEY) || "Usuário";
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USERNAME_KEY);
        window.location.href = "login.html";
    },

    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = "login.html";
        }
    }
};

const Api = {
    request(baseUrl, path, options = {}) {
        const headers = Object.assign(
            { "Content-Type": "application/json" },
            options.headers || {}
        );

        const token = Auth.getToken();
        if (token) {
            headers["Authorization"] = "Bearer " + token;
        }

        return fetch(baseUrl + path, Object.assign({}, options, { headers }))
            .then(function (response) {
                if (response.status === 401) {
                    Auth.logout();
                    return Promise.reject(new Error("Sessão expirada."));
                }
                if (!response.ok) {
                    return response.json().catch(function () {
                        return { message: "Erro na requisição (" + response.status + ")." };
                    }).then(function (body) {
                        return Promise.reject(body);
                    });
                }
                if (response.status === 204) {
                    return null;
                }
                const contentType = response.headers.get("content-type") || "";
                if (contentType.includes("application/json")) {
                    return response.json();
                }
                return response.blob();
            });
    },

    ms1(path, options) {
        return this.request(API_CONFIG.MS1_URL, path, options);
    },

    ms2(path, options) {
        return this.request(API_CONFIG.MS2_URL, path, options);
    }
};

function formatCurrency(value) {
    if (value === null || value === undefined) return "-";
    return Number(value).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatDate(isoString) {
    if (!isoString) return "-";
    const date = new Date(isoString);
    return date.toLocaleString("pt-BR");
}

function showAlert(containerId, message, type) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML =
        '<div class="alert alert-' + (type || "danger") + ' alert-dismissible fade show" role="alert">' +
        message +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span></button></div>';
}

function extractErrorMessage(error) {
    if (!error) return "Erro inesperado.";
    if (error.message) return error.message;
    if (error.fieldErrors) {
        return Object.entries(error.fieldErrors).map(function (entry) {
            return entry[0] + ": " + entry[1];
        }).join("<br>");
    }
    return "Erro inesperado.";
}
